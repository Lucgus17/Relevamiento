package org.example;

import java.time.LocalDateTime;


public class SesionRelevamiento {

    private  String nombreRelevamiento;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private boolean activa;
    private Relevamiento relevamiento;


    public void iniciar(){
        if (!activa) {
            activa = true;
            this.fechaHoraInicio = LocalDateTime.now();
            System.out.println("Sesion iniciada a las:" + this.fechaHoraInicio);
        }
    }

    public void finalizar(LocalDateTime now) {
        if (activa){
           activa = false;
           this.fechaHoraFin = LocalDateTime.now();
           System.out.println("Sesion finalizar a las:" + this.fechaHoraFin);
        }

    }

    /*Get y sets*/

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public String getNombreRelevamiento() {
        return nombreRelevamiento;
    }

    public void setNombreRelevamiento(String nombreRelevamiento) {
        this.nombreRelevamiento = nombreRelevamiento;
    }
}
