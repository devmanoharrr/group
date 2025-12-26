package kf7014.crowdsourceddata.model;

public class MeasurementSet {
    private Double temperatureCelsius;
    private Double ph;
    private Double alkalinityMgPerL;
    private Double turbidityNtu;

    public Double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public void setTemperatureCelsius(Double temperatureCelsius) {
        this.temperatureCelsius = temperatureCelsius;
    }

    public Double getPh() {
        return ph;
    }

    public void setPh(Double ph) {
        this.ph = ph;
    }

    public Double getAlkalinityMgPerL() {
        return alkalinityMgPerL;
    }

    public void setAlkalinityMgPerL(Double alkalinityMgPerL) {
        this.alkalinityMgPerL = alkalinityMgPerL;
    }

    public Double getTurbidityNtu() {
        return turbidityNtu;
    }

    public void setTurbidityNtu(Double turbidityNtu) {
        this.turbidityNtu = turbidityNtu;
    }
}


