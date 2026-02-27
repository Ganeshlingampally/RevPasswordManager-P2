package com.revpasswordmanager.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class GeneratePasswordRequest {
    @NotNull
    @Min(8)
    private Integer length;
    private boolean includeUppercase = true;
    private boolean includeLowercase = true;
    private boolean includeNumbers = true;
    private boolean includeSymbols = true;
    private int options = 1;

    public Integer getLength() { return length; }
    public void setLength(Integer length) { this.length = length; }
    public boolean isIncludeUppercase() { return includeUppercase; }
    public void setIncludeUppercase(boolean includeUppercase) { this.includeUppercase = includeUppercase; }
    public boolean isIncludeLowercase() { return includeLowercase; }
    public void setIncludeLowercase(boolean includeLowercase) { this.includeLowercase = includeLowercase; }
    public boolean isIncludeNumbers() { return includeNumbers; }
    public void setIncludeNumbers(boolean includeNumbers) { this.includeNumbers = includeNumbers; }
    public boolean isIncludeSymbols() { return includeSymbols; }
    public void setIncludeSymbols(boolean includeSymbols) { this.includeSymbols = includeSymbols; }
    public int getOptions() { return options; }
    public void setOptions(int options) { this.options = options; }
}

