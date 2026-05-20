package de.hexfieldsstudio.hexfieldsdominion.config.filter;

import jakarta.servlet.FilterChain;

public class FilterITUtils {

    public static FilterChain createDummyFilterChain() {
        return (request, response) -> {};
    }

}
