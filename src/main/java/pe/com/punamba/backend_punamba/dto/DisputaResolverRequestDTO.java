package pe.com.punamba.backend_punamba.dto;

import pe.com.punamba.backend_punamba.entity.Disputa;

import java.math.BigDecimal;

public class DisputaResolverRequestDTO {

    private Disputa.EstadoDisputa nuevoEstado;
    private BigDecimal montoReembolso;
    private String observacionAdmin;
    private String resolucionFinal;

    public Disputa.EstadoDisputa getNuevoEstado() {
        return nuevoEstado;
    }

    public void setNuevoEstado(Disputa.EstadoDisputa nuevoEstado) {
        this.nuevoEstado = nuevoEstado;
    }

    public BigDecimal getMontoReembolso() {
        return montoReembolso;
    }

    public void setMontoReembolso(BigDecimal montoReembolso) {
        this.montoReembolso = montoReembolso;
    }

    public String getObservacionAdmin() {
        return observacionAdmin;
    }

    public void setObservacionAdmin(String observacionAdmin) {
        this.observacionAdmin = observacionAdmin;
    }

    public String getResolucionFinal() {
        return resolucionFinal;
    }

    public void setResolucionFinal(String resolucionFinal) {
        this.resolucionFinal = resolucionFinal;
    }
}