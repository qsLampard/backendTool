
public abstract class SettingListener {

    public final void changed(List<String> keyName, long updatedTime) {
    	//do some common things
        settingChanged(keyName, updatedTime);
    }

    public abstract void settingChanged(List<String> key, long updatedTime);

}
