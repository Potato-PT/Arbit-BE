package com.arbit.app.auth.service;

public record ResidentialCoordinates(Double latitude, Double longitude) {

    public static ResidentialCoordinates empty() {
        return new ResidentialCoordinates(null, null);
    }
}
