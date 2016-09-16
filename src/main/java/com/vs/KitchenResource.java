package com.vs;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/kitchen")
public class KitchenResource {

	private String secretKey = "HkO9.3o'~I5A/uyLm}oYEl2oq<g`d$";

	@Autowired
	private KitchenService kitchenService;

	@RequestMapping(value = "/populatedb", method = RequestMethod.POST, consumes = { "application/json" })
	public HttpStatus populateDB(@RequestBody PopulateDBParams populateDBParams) {
		if (populateDBParams.getSecretKey().equals(secretKey)) {
			try {
				Scanner f = new Scanner(new File("src/main/resources/us_zips.txt"));
				while (f.hasNextLine()) {
					String line = f.nextLine();
					// Tokenize and call mongo api to persist collection					
					kitchenService.insertEntry(line);
				}
				f.close();
			} catch (IOException ex) {
				return HttpStatus.INTERNAL_SERVER_ERROR;
			}
		}
		return HttpStatus.FORBIDDEN;
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST, consumes = {
			"application/json" }, produces = "application/json")
	public HttpStatus create(@RequestBody KitchenParams params) {
		// Query for longitude and latitude from zips db
		List<Geonames> geonames = kitchenService.findGeoLocationByZip(params.getAddress().getZipcode());
		Float longitude = geonames.get(0).getLocation().getLongitude();
		Float latitude = geonames.get(0).getLocation().getLatitude();
		// Get longitude and latitude from zips db;
		params.getAddress().getGeoLocation().setLongitude(longitude);
		params.getAddress().getGeoLocation().setLatitude(latitude);
		kitchenService.createKitchen(params);
		return HttpStatus.OK;
	}

	@RequestMapping(value = "/findAllKitchens", method = RequestMethod.GET)
	public List<Kitchen> findAllKitchens() {
		return kitchenService.findAll();
	}

	@RequestMapping(value = "/findKitchensByName", method = RequestMethod.GET)
	public List<Kitchen> findKitchensByName() {
		return kitchenService.findByName("gopi's kitchen");
	}

	@RequestMapping(value = "/findKitchensByZip", method = RequestMethod.GET)
	public List<Kitchen> findKitchensByZip() {
		return kitchenService.findByZip(75024);
	}

	@RequestMapping(value = "/findKitchensWithinGivenRadius", method = RequestMethod.GET)
	public List<Kitchen> findKitchensWithinGivenRadius(	@RequestParam(value = "zip") int zip, @RequestParam(value = "distance") int distance) {
		return kitchenService.findKitchensWithinGivenRadius(zip, distance);
	}

}
