package com.teamunify.i18n.settings;

import java.util.Locale;

class DFKey {
    private int formatID;
    private String localeName;

    public DFKey(int formatID, Locale l) {
        this.formatID = formatID;
        this.localeName = l.toString();
    }

    private DFKey(int formatID, String lname) {
        this.formatID = formatID;
        this.localeName = lname;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + formatID;
        result = prime * result + ((localeName == null) ? 0 : localeName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        DFKey other = (DFKey) obj;
        if (formatID != other.formatID) return false;
        if (localeName == null) {
            if (other.localeName != null) return false;
        } else if (!localeName.equals(other.localeName)) return false;
        return true;
    }

    public DFKey withoutCountry() {
        final int idx = this.localeName.indexOf("_");
        if (idx > 0) {
            final String lang = this.localeName.substring(0, idx);
            return new DFKey(this.formatID, lang);
        }
        return this;
    }
}
