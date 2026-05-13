/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mapademo;

/**
 *
 * @author amira
 */

import java.io.Serializable;

public class Anotacion implements Serializable {
    private double x; 
    private double y; 
    private String texto;

    public Anotacion(double x, double y, String texto) {
        this.x = x;
        this.y = y;
        this.texto = texto;
    }

    // Generar Getters y Setters (Alt+Insert en NetBeans)
    public double getX() { return x; }
    public double getY() { return y; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
}