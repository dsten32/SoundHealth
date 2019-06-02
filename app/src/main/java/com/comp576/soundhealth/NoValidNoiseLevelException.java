package com.comp576.soundhealth;

class NoValidNoiseLevelException extends Exception {
    public NoValidNoiseLevelException() {
        super();
    }

    public NoValidNoiseLevelException(String message) {
        super(message);
    }

    public NoValidNoiseLevelException(double message) {
        super(String.valueOf(message));
    }
}
