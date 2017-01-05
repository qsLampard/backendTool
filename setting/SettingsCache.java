import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;

import javax.inject.Inject;

/*
SettingCache read inst settings every "DEFAULT_REFRESH_INTERVAL_SECONDS"
and get setting by "settingsCache.get(Setting.ProfileProxyServerUri)"
It can add setting listener to monitor setting changes and kills any listener thread which runs over "DEFAULT_LISTENER_WAIT_SECONDS"
 */
public class SettingsCache {
    public static final String SETTINGS_REFRESH_INTERVAL_SECONDS = "SettingsRefreshIntervalSeconds";
    private final ExecutorService notificationService = Executors.newSingleThreadExecutor(
            new BasicThreadFactory.Builder()
                    .namingPattern("SettingChangeNotifier-%d")
                    .daemon(false)
                    .priority(Thread.NORM_PRIORITY)
                    .build());

    private final Map<String, SettingValue> overrides;
    private final Map<String, List<SettingListener>> listenersMap;

    private final static int DEFAULT_REFRESH_INTERVAL_SECONDS = 900;
    private final static int DEFAULT_LISTENER_WAIT_SECONDS = 30;
    private final ScheduledExecutorService refreshService
            = Executors.newSingleThreadScheduledExecutor(new BasicThreadFactory.Builder()
            .namingPattern("SettingsRefresher-%d")
            .daemon(false)
            .priority(Thread.NORM_PRIORITY)
            .build());

    private static SettingsCache instance;

    private SettingsCache() throws Exception {
        overrides = new ConcurrentHashMap<String, SettingValue>();
        listenersMap = new ConcurrentHashMap<String, List<SettingListener>>();
        final Map<String, SettingValue> props = SettingsReader.getSettings();
        refreshAll(props);
        int refreshIntervalSeconds = Integer.parseInt(System.getProperty(SETTINGS_REFRESH_INTERVAL_SECONDS, String.valueOf(DEFAULT_REFRESH_INTERVAL_SECONDS)));
        refreshService.scheduleWithFixedDelay(() -> refresh(), refreshIntervalSeconds, refreshIntervalSeconds, TimeUnit.SECONDS);
    }

    public static SettingsCache getInstance() throws Exception {
        if (instance == null) {
            instance = new SettingsCache();
        }
        return instance;
    }

    public SettingValue get(String key) {
        if (overrides.containsKey(key)) {
            return overrides.get(key);
        } else if (SettingsReader.SETTINGNAMES.contains(key)) {
            return Settings.valueOf(key).getValue();
        }
        return null;
    }

    public void refreshAll(Map<String, SettingValue> props) {
        long updatedTime = System.currentTimeMillis();
        //several settings may have same SettingListener
        Map<SettingListener, List<String>> listenersMap = new HashMap<SettingListener, List<String>>();
        for (Map.Entry<String, SettingValue> entry : props.entrySet()) {
            String key = entry.getKey();
            SettingValue settingValue = entry.getValue();
            SettingValue originValue = get(key);
            if (originValue == null || !originValue.equals(settingValue)) {
                overrides.put(key, settingValue);
                Set<SettingListener> listeners = getCopyOfListeners(key);
                listeners.stream().forEach(listener -> {
                    if (!listenersMap.containsKey(listener)) {
                        listenersMap.put(listener, new ArrayList<String>());
                    }
                    listenersMap.get(listener).add(key);
                });
            }
        }
        listenersMap.entrySet().stream().forEach(entry -> notifyChange(entry.getKey(), entry.getValue(), updatedTime));
    }

    public void put(String key, SettingValue value) {
        if (overrides.containsKey(key) && overrides.get(key).stringValue.equals(value.stringValue)) {
            return;
        }
        if (SettingsReader.SETTINGNAMES.contains(key) && Settings.valueOf(key).getValue().stringValue.equals(value.stringValue)) {
            return;
        }
        Set<SettingListener> listeners = getCopyOfListeners(key);

        overrides.put(key, value);
        notifyChange(key, System.currentTimeMillis(), listeners);
    }

    public void remove(String key) {
        overrides.remove(key);
    }

    public void addListener(String key, SettingListener l) {
        synchronized (listenersMap) {
            if (!listenersMap.containsKey(key)) {
                listenersMap.put(key, new CopyOnWriteArraySet<SettingListener>());
            }
            listenersMap.get(key).add(l);
        }
    }

    public void removeListener(String key, SettingListener l) {
        synchronized (listenersMap) {
            if (listenersMap.containsKey(key)) {
                listenersMap.get(key).remove(l);
            }
        }
    }

    public void removeListener(String key) {
        synchronized (listenersMap) {
            listenersMap.remove(key);
        }
    }

    private Set<SettingListener> getCopyOfListeners(String key) {
        Set<SettingListener> listeners;
        synchronized (listenersMap) {
            if (listenersMap.containsKey(key)) {
                listeners = ImmutableSet.copyOf(listenersMap.get(key));
            } else {
                listeners = new HashSet<SettingListener>();
            }
        }
        return listeners;
    }

    private void notifyChange(final SettingListener listener, final List<String> key, final long updatedTime) {
        ChangeNotification change = new ChangeNotification(key, updatedTime, listener);
        //Add timeout for listener threads
        Future<?> listernerThread = notificationService.submit(change);
        try {
            listernerThread.get(DEFAULT_LISTENER_WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            //logger.error("Error in listener for keys {}", key, e);
        }
    }

    private class ChangeNotification implements Runnable {
        private final List<String> key;
        private final long updatedTime;
        private final SettingListener changeListener;

        public ChangeNotification(final List<String> key, final long updatedTime, final SettingListener listener) {
            this.key = key;
            this.updatedTime = updatedTime;
            this.changeListener = listener;
        }

        @Override
        public void run() {
            try {
                changeListener.accept(key, updatedTime);
            } catch (Throwable t) {
                //logger.error("Error in listener for keys {}: {}", key, changeListener, t);
            }
        }
    }

    private void refresh() {
        try {
            Map<String, SettingValue> props = SettingsReader.getSettings();
            refreshAll(props);
        } catch (Exception e) {
            //logger.error("Settings load failed");
        }
    }
}