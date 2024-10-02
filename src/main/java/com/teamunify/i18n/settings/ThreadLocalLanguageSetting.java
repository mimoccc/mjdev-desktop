package com.teamunify.i18n.settings;

import com.teamunify.i18n.I;

/**
 * Thread local variable for holding the current request thread's language preferences, so we can more succinctly call
 * translation functions. Without this, we'd have to pass the session to each and every call of tr(). @see
 * AbstractLocaleFilter.
 */
public class ThreadLocalLanguageSetting extends ThreadLocal<LanguageSetting> {
    @Override
    protected LanguageSetting initialValue() {
        return I.getDefaultLanguage();
    }
}
