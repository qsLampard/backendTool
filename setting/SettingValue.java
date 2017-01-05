
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SettingValue {

    String stringValue;
    private static final Logger LOG = YODALoggerContext.getLogger(SettingsReader.class);

    public SettingValue(String value) {
        this.stringValue = value;
    }

    public String getString() {
        return stringValue;
    }

    public Integer getInt() {
        return Integer.parseInt(stringValue);
    }

    public Long getLong() {
        return Long.parseLong(stringValue);
    }

    public Double getDouble() {
        return Double.parseDouble(stringValue);
    }

    public Boolean getBoolean() {
        return Boolean.parseBoolean(stringValue);
    }

    public Set<Long> getCommaSeparatedNumbers() {
        Set<Long> result = new HashSet<Long>();
        for (String seperatedString : getCommaSeparatedStrings()) {
            result.add(Long.parseLong(seperatedString));
        }
        return result;
    }

    public List<Long> getCommaSeparatedNumbersAsList() {
        List<Long> result = new ArrayList<Long>();
        for (String seperatedString : getCommaSeparatedStringsAsList()) {
            result.add(Long.parseLong(seperatedString));
        }
        return result;
    }
    
    public Set<Double> getCommaSeparatedDoubles() {
        Set<Double> result = new HashSet<Double>();
        for (String seperatedString : getCommaSeparatedStrings()) {
            result.add(Double.parseDouble(seperatedString));
        }
        return result;
    }
    
    public List<Double> getCommaSeparatedDoublesAsList() {
        List<Double> result = new ArrayList<Double>();
        for (String seperatedString : getCommaSeparatedStringsAsList()) {
            result.add(Double.parseDouble(seperatedString));
        }
        return result;
    }

    public Set<Integer> getCommaSeparatedInts() {
        Set<Integer> result = new HashSet<Integer>();
        for (String seperatedString : getCommaSeparatedStrings()) {
            result.add(Integer.parseInt(seperatedString));
        }
        return result;
    }

    public List<Integer> getCommaSeparatedIntsAsList() {
        List<Integer> result = new ArrayList<Integer>();
        for (String seperatedString : getCommaSeparatedStringsAsList()) {
            result.add(Integer.parseInt(seperatedString));
        }
        return result;
    }

    public List<String> getCommaSeparatedStringsAsList() {
        List<String> result = new ArrayList<String>();
        if (stringValue == null) {
            LOG.error("Couldn't parse value, skipping");
            return result;
        }
        String[] parsedStrings = stringValue.trim().split(",");
        for (String parsedString : parsedStrings) {
            if (parsedStrings.length != 0) {
                result.add(parsedString);
            }
        }
        return result;
    }

    public Set<String> getCommaSeparatedStrings() {
        Set<String> result = new HashSet<String>();
        if (stringValue == null) {
            LOG.error("Couldn't parse value, skipping");
            return result;
        }
        String[] parsedStrings = stringValue.trim().split(",");
        for (String parsedString : parsedStrings) {
            if (parsedStrings.length != 0) {
                result.add(parsedString);
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((stringValue == null) ? 0 : stringValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SettingValue other = (SettingValue) obj;
        if (stringValue == null) {
            if (other.stringValue != null) {
                return false;
            }
        } else if (!stringValue.equals(other.stringValue)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return stringValue;
    }
}
