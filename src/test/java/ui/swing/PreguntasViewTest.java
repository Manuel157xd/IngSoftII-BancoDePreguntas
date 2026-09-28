package ui.swing;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import javax.swing.JComboBox;

import org.junit.jupiter.api.Test;

class PreguntasViewTest {

    @Test
    void debeIgnorarSeleccionPorDefectoCuandoElComboEstaVacio() {
        JComboBox<String> difficulty = new JComboBox<>();

        assertDoesNotThrow(() -> PreguntasView.seleccionarPrimeraOpcionSiExiste(difficulty));
    }
}
