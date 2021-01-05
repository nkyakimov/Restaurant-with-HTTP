package restaurant.server.handlers;

public class Request {
    private String requestParam;
    private String requestArgs;

    public Request() {
        requestParam = "";
        requestArgs = "";
    }

    public Request(String requestParam, String requestArgs) {
        this.requestParam = requestParam;
        this.requestArgs = requestArgs;
    }

    public Request(String info) {
        info = info.strip();
        var split = info.split(" *= *", 2);
        if (split.length == 1) {
            requestParam = info;
            requestArgs = "";
        } else if (split.length == 2) {
            requestParam = split[0];
            requestArgs = split[1];
        } else {
            requestParam = "";
            requestArgs = "";
        }
    }

    @Override
    public String toString() {
        return "Request{" +
                "requestParam='" + requestParam + '\'' +
                ", requestArgs='" + requestArgs + '\'' +
                '}';
    }

    public String getRequestParam() {
        return requestParam;
    }

    public void setRequestParam(String requestParam) {
        this.requestParam = requestParam;
    }

    public String getRequestArgs() {
        return requestArgs;
    }

    public void setRequestArgs(String requestArgs) {
        this.requestArgs = requestArgs;
    }

}
