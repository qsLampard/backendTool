SettingCache reads settings from settings reader every "DEFAULT_REFRESH_INTERVAL_SECONDS"
and gets setting by "settingsCache.get("Some Key")"
Setting listener can be added to monitor setting changes and SettingCache kills any listener thread which runs over "DEFAULT_LISTENER_WAIT_SECONDS"

Settings Reader Reads settings from command setting and config file