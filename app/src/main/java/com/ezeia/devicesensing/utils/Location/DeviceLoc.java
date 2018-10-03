package com.ezeia.devicesensing.utils.Location;

import java.io.Serializable;

class DeviceLoc implements Serializable
{
    private Double latitude;

    private Double longitude;

    private Double accuracy;

    private Double altitude;

    private Double bearing;

    private Double speed;

    private Boolean hasAccuracy;

    private Boolean hasAltitude;

    private Boolean hasBearing;

    private Boolean hasSpeed;

    private Boolean isFromMock;

    private Long elaspedTime;

    private Long getTime;

    private String provider;

    private String addressLine1;

    private String city;

    private String state;

    private String countryCode;

    private String postalCode;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Double getBearing() {
        return bearing;
    }

    public void setBearing(Double bearing) {
        this.bearing = bearing;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Boolean getHasAccuracy() {
        return hasAccuracy;
    }

    public void setHasAccuracy(Boolean hasAccuracy) {
        this.hasAccuracy = hasAccuracy;
    }

    public Boolean getHasAltitude() {
        return hasAltitude;
    }

    public void setHasAltitude(Boolean hasAltitude) {
        this.hasAltitude = hasAltitude;
    }

    public Boolean getHasBearing() {
        return hasBearing;
    }

    public void setHasBearing(Boolean hasBearing) {
        this.hasBearing = hasBearing;
    }

    public Boolean getHasSpeed() {
        return hasSpeed;
    }

    public void setHasSpeed(Boolean hasSpeed) {
        this.hasSpeed = hasSpeed;
    }

    public Boolean getIsFromMock() {
        return isFromMock;
    }

    public void setIsFromMock(Boolean isFromMock) {
        this.isFromMock = isFromMock;
    }

    public Long getElapsedTime() {
        return elaspedTime;
    }

    public void setElaspedTime(Long elaspedTime) {
        this.elaspedTime = elaspedTime;
    }

    public Long getTime() {
        return getTime;
    }

    public void setTime(Long getTime) {
        this.getTime = getTime;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
