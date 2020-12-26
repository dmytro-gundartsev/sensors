package org.gundartsev.edu.sensors.status;

import org.gundartsev.edu.sensors.domain.StatusData;
import org.springframework.lang.NonNull;

import java.util.UUID;

public interface ISensorStatusService {
    @NonNull
    StatusData getStatus(UUID uuid);
}
