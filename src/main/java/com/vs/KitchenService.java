package com.vs;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class KitchenService {

	@Autowired
	private MongoOperations mongoOperation;

	public HttpStatus createKitchen(KitchenParams params) {
		try {
			Kitchen k = new Kitchen();
			Address add = params.getAddress();
			k.setAddress(add);
			String owner = params.getOwner();
			k.setOwner(owner);
			mongoOperation.save(k);
		} catch (Exception e) {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return HttpStatus.OK;
	}
	
	public HttpStatus insertEntry(String line) {
		try {
			String[] tokens = line.split(Pattern.quote(","));
			String country = "US";
			int zip = Integer.parseInt(tokens[0]);
			String city = tokens[1];
			String state = tokens[2];
			Float longitude = Float.parseFloat(tokens[4]);
			Float latitude = Float.parseFloat(tokens[3]);
			Geonames geo = new Geonames();
			geo.setCountry(country);
			geo.setCity(city);
			geo.setState(state);
			geo.setZip(zip);
			geo.setCounty(tokens[5]);
			Location loc = new Location();
			loc.setLongitude(longitude);
			loc.setLatitude(latitude);
			geo.setLocation(loc);
			mongoOperation.save(geo);			
			
		} catch (Exception e) {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return HttpStatus.OK;
	}

	public List<Kitchen> findAll() {
		return mongoOperation.findAll(Kitchen.class);
	}

	public List<Kitchen> findByName(String name) {

		return mongoOperation.find(new Query(Criteria.where("owner").is(name)), Kitchen.class);
	}

	public List<Kitchen> findByZip(int zip) {

		return mongoOperation.find(new Query(Criteria.where("zipcode").is(zip)), Kitchen.class);
	}
	
	public List<Geonames> findGeoLocationByZip(int zip) {
		Query q = new Query();
		q.addCriteria(Criteria.where("zip").is(zip));
		List<Geonames> locations = mongoOperation.find(q, Geonames.class);
		return locations;
	}

	public List<Kitchen> findKitchensWithinGivenRadius(int zip, int distance) {
		// Get latitude and longitude based on zip
		List<Geonames> geonames = findGeoLocationByZip(zip);
		Float longitude = geonames.get(0).getLocation().getLongitude();
		Float latitude = geonames.get(0).getLocation().getLatitude();
		Point basePoint = new Point(longitude, latitude);
		Distance radius = new Distance(distance, Metrics.MILES);
		Circle area = new Circle(basePoint, radius);
		Query q = new Query();
		q.addCriteria(Criteria.where("address.geoLocation").withinSphere(area));
		List<Kitchen> kitchens = mongoOperation.find(q, Kitchen.class);
		return kitchens;
	}		
}
