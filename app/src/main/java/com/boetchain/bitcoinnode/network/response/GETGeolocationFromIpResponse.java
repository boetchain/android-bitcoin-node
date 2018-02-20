package com.boetchain.bitcoinnode.network.response;

/**
 * Created by Ross Badenhorst.
 */
public class GETGeolocationFromIpResponse extends BaseResponse {

    public String status;
    public String city = "";
    public String country = "";
    public String countryCode = "";
    public String isp = "";
    public double lat = 0;
    public double lon = 0;
    public String region = "";
    public String regionName = "";
}
