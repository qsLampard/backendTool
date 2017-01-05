import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class SettingsReader {
    private final static String SETTINGS_FILE = "config/setting.properties";

    private static final String INST = "/usr/local/bin/inst";
    private static final String CMD = INST + " set";
    private static final String PACKAGE_NAME = "myService";
    public static final HashSet<String> SETTINGNAMES = new HashSet<String>();     //check if setting name exists to avoid exception

    //http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html?page=2
    private static final ExecutorService instReader = Executors.newSingleThreadExecutor(
            new BasicThreadFactory.Builder()
                    .namingPattern("instReader-%d")
                    .daemon(false)
                    .priority(Thread.NORM_PRIORITY)
                    .build());

    public static Map<String, SettingValue> loadSettingsFromFile(String settingFile) {
        loadSettingName();
        Map<String, SettingValue> changes = new HashMap<String, SettingValue>();
        try {
            File file = new File(settingFile);
            if (file.exists()) {
                Properties p = new Properties();
                InputStream in = new FileInputStream(file);
                try {
                    p.load(in);
                } finally {
                    in.close();
                }
                for (Map.Entry<Object, Object> prop : p.entrySet()) {
                    String name = (String) prop.getKey();
                    String value = (String) prop.getValue();

                    changes.put(name, new SettingValue(value));
                }
            }
        } catch (Exception ioe) {
            //LOG.error("Error loading local settings", ioe);
        }
        return changes;
    }

    public static Map<String, SettingValue> loadSettingsFromFile() {
        return loadSettingsFromFile(SETTINGS_FILE);
    }

    public static Map<String, SettingValue> loadSettingsFromCMD() throws Exception {
        loadSettingName();
        Map<String, SettingValue> changes = new HashMap<String, SettingValue>();
        File inst = new File(INST);
        if (!inst.exists()) { // can check for execute permission in JDK 1.6
            return changes;
        }

        // execute "inst set' and filter the result
        final Runtime rt = Runtime.getRuntime();
        final Process p = rt.exec(CMD);

        final Future<Properties> propFuture = instReader.submit(new CMDReader(p.getInputStream()));
        // Wait for the spawned process to terminate
        p.waitFor();
        final Properties property = propFuture.get();

        Arrays.stream(Setting.values()).forEach(setting -> {
            if (property.get(setting.name()) != null) {
                changes.put(setting.name(), new SettingValue((String) property.get(setting.name())));
            }
        });
        return changes;
    }

    private static void loadSettingName() {
        if (!SETTINGNAMES.isEmpty()) {
            return;
        }
        for (Settings setting : EnumSet.allOf(Settings.class)) {
            SETTINGNAMES.add(setting.name());
        }
    }

    public static Map<String, SettingValue> getSettings() throws Exception {
        Map<String, SettingValue> changes = new HashMap<String, SettingValue>();
        changes.putAll(loadSettingsFromCMD());
        changes.putAll(loadSettingsFromFile());
        return changes;
    }

    static class CMDReader implements Callable<Properties> {
        private final InputStream inputStream;

        public CMDReader(final InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public Properties call() throws Exception {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            Properties property = new Properties();
            while ((line = br.readLine()) != null) {
                String[] values = line.split("\\.", 2);
                if (PACKAGE_NAME.equals(values[0])) {
                    String[] keyValuePair = values[1].split(":", 2);
                    property.put(keyValuePair[0], keyValuePair[1]);
                }
            }
            return property;
        }
    }
}
