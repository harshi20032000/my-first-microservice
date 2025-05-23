package com.ms.booking.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.ms.booking.entity.BookingDetails;
import com.ms.booking.entity.Flight;
import com.ms.booking.entity.Passenger;
import com.ms.booking.entity.PassengerDetails;
import com.ms.booking.entity.Ticket;
import com.ms.booking.exception.ARSServiceException;
import com.ms.booking.exception.ExceptionConstants;
import com.ms.booking.exception.InfyGoServiceException;
import com.ms.booking.service.TicketService;
import com.ms.booking.utility.ClientErrorInformation;

@RestController

@RequestMapping("/book")
public class BookingController {

	protected Logger logger = Logger.getLogger(BookingController.class.getName());

	@Value("${flight.api.url}")
	private String flightApiUrl;
	
	@Value("${passenger.api.url}")
	private String passengerApiUrl;

	@Autowired
	private TicketService ticketService;
	private Ticket ticket;

	@Autowired
	private RestTemplate restTemplate;

	public BookingController() {
		ticket = new Ticket();
	}

	@PostMapping(value = "/{flightId}/{username}", produces = "application/json", consumes = "application/json")
	public ResponseEntity<BookingDetails> bookFlight(@PathVariable("flightId") String flightId,
			@RequestBody PassengerDetails passengerDetails, @PathVariable("username") String username, Errors errors)
			throws InfyGoServiceException, ARSServiceException {

		if (errors.hasErrors()) {
			return new ResponseEntity(new ClientErrorInformation(HttpStatus.BAD_REQUEST.value(),
					errors.getFieldError("passengerList").getDefaultMessage()), HttpStatus.BAD_REQUEST);
		}
		if (passengerDetails.getPassengerList().isEmpty())
			throw new InfyGoServiceException(ExceptionConstants.PASSENGER_LIST_EMPTY.toString());

		List<Passenger> passengerList = new ArrayList<>();
		for (Passenger passengers : passengerDetails.getPassengerList()) {
			passengerList.add(passengers);

		}

		logger.log(Level.INFO, "Book Flight method ");

		logger.log(Level.INFO, passengerDetails.toString());
		int pnr = (int) (Math.random() * 1858955);

		ticket.setPnr(pnr);

		Flight flight = restTemplate.getForEntity(flightApiUrl + flightId, Flight.class).getBody();

		double fare = flight.getFare();
		double totalFare = fare * (passengerDetails.getPassengerList().size());

		BookingDetails bookingDetails = new BookingDetails();
		bookingDetails.setPassengerList(passengerDetails.getPassengerList());
		bookingDetails.setPnr(pnr);
		bookingDetails.setTotalFare(totalFare);
		ticket.setBookingDate(new Date());
		ticket.setDepartureDate(flight.getFlightAvailableDate());
		ticket.setDepartureTime(flight.getDepartureTime());
		ticket.setFlightId(flight.getFlightId());
		ticket.setUserId(username);
		ticket.setTotalFare(totalFare);
		int noOfSeats = passengerDetails.getPassengerList().size();
		ticket.setNoOfSeats(noOfSeats);
		ticketService.createTicket(ticket);

		addPassengers(bookingDetails.getPassengerList(), pnr);

		restTemplate.getForEntity(flightApiUrl + flightId + "/" + noOfSeats, Flight.class);

		return new ResponseEntity<>(bookingDetails, HttpStatus.OK);

	}

	private void addPassengers(List<Passenger> passengers, int pnr) {
		for (Passenger passenger : passengers) {
			passenger.setTicketPnr(pnr);
		}
		restTemplate.postForEntity(passengerApiUrl, passengers, Boolean.class);
	}
}
