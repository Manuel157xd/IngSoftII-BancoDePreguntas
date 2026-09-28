package com.taller2.presentacion.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.junit.jupiter.api.Test;

class PreguntasViewTest {

    @Test
    void actualizaOpcionesCorrectasMientrasSeEscribenSinGuardar() {
        JTextField options = new JTextField();
        JComboBox<Integer> correctOption = new JComboBox<>();
        PreguntasView.vincularOpcionesCorrectas(options, correctOption);

        options.setText("Opcion A|Opcion B|Opcion C|Opcion D");

        assertEquals(4, correctOption.getItemCount());
        correctOption.setSelectedIndex(2);
        assertEquals(2, correctOption.getSelectedItem());
    }

    @Test
    void noOfreceOpcionVaciaMientrasSeEscribeElSeparador() {
        JTextField options = new JTextField();
        JComboBox<Integer> correctOption = new JComboBox<>();
        PreguntasView.vincularOpcionesCorrectas(options, correctOption);

        options.setText("Opcion A|Opcion B|");

        assertEquals(2, correctOption.getItemCount());
    }

    @Test
    void debeIgnorarSeleccionPorDefectoCuandoElComboEstaVacio() {
        JComboBox<String> difficulty = new JComboBox<>();

        assertDoesNotThrow(() -> PreguntasView.seleccionarPrimeraOpcionSiExiste(difficulty));
    }
}
