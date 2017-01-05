
public enum Settings {
    TestStringSetting("Are you ok?", 
            "TestStringSetting"),
    TestDoubleSetting("34.5", 
            "TestDoubleSetting"),
    TestMultiIntSetting("1,4,7", 
            "TestMultiIntSetting")
    ;

    private SettingValue value;
    private final String description;
    
    private Settings(String defaultValue, String description) {
        this.value = new SettingValue(defaultValue);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns the current value of the setting as a String (or the default value if none is set)
     */
    public SettingValue getValue() {
        return value;
    }
}
