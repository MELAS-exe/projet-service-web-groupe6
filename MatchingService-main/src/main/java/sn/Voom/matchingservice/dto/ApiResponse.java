package sn.Voom.matchingservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean succes;
    private final String message;
    private final T donnees;
    private final LocalDateTime horodatage;

    public static <T> ApiResponse<T> ok(T donnees) {
        return ApiResponse.<T>builder().succes(true).donnees(donnees).horodatage(LocalDateTime.now()).build();
    }
    public static <T> ApiResponse<T> ok(T donnees, String message) {
        return ApiResponse.<T>builder().succes(true).message(message).donnees(donnees).horodatage(LocalDateTime.now()).build();
    }
    public static <T> ApiResponse<T> erreur(String message) {
        return ApiResponse.<T>builder().succes(false).message(message).horodatage(LocalDateTime.now()).build();
    }
}
