package com.boetchain.bitcoinnode.network.response;

/**
 * Created by rossbadenhorst on 2018/02/08.
 */

public class GETExternalIpResponse extends BaseResponse {

    /**
     * External IP returned from server.
     * Empty as default, so it can never be null
     */
    public String ip = "";
}
