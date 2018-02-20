package com.boetchain.bitcoinnode.network.response;

/**
 * Created by Ross Badenhorst.
 */
public class GETGeolocationFromIpResponse extends BaseResponse {

    public String success;
    public String city = "";
    public String country = "";
    public String countryCode = "";
    public String isp = "";
    public long lat;
    public long lon;
    public String region;
    public String regionName;
}
