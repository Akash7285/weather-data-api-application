package com.akash.weather_data_api;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class GeoCodingResponseDTO {

	private List<Location> results;
	
	public List<Location> getResults() {
		return results;
	}

	public void setResults(List<Location> results) {
		this.results = results;
	}

	@Data
	public static class Location{
		private double latitude;
		private double longitude;
		public double getLatitude() {
			return latitude;
		}
		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}
		public double getLongitude() {
			return longitude;
		}
		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}
		
	}

	
}
