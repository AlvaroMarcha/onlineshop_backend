package es.marcha.backend.model.enums;

/**
 * Representa el estado del ciclo de vida de un pago.
 *
 * Un pago es un evento financiero inmutable.
 * Una vez que un pago alcanza un estado final, NO debe modificarse.
 * Cualquier nueva acción financiera (reintento, reembolso, pago parcial)
 * debe representarse como un NUEVO registro de pago.
 */
public enum PaymentStatus {
    /**
     * El pago ha sido creado en el sistema,
     * pero TODAVÍA no se ha enviado al proveedor de pago.
     *
     * No se ha realizado ninguna operación financiera.
     */
    CREATED,

    /**
     * El pago ha sido enviado al proveedor y
     * se encuentra actualmente en proceso.
     *
     * Común en métodos de pago asíncronos.
     */
    PENDING,

    /**
     * El importe del pago ha sido autorizado por el banco,
     * lo que significa que los fondos están reservados
     * pero AÚN no han sido capturados.
     *
     * Se usa principalmente en pagos con tarjeta.
     */
    AUTHORIZED,

    /**
     * El pago se ha completado correctamente.
     *
     * El dinero ha sido capturado y transferido.
     * Este es un estado FINAL e INMUTABLE.
     */
    SUCCESS,

    /**
     * El intento de pago ha fallado.
     *
     * Posibles causas:
     * - Fondos insuficientes
     * - Error del proveedor
     * - Rechazo del usuario
     *
     * Este pago nunca llegará a completarse.
     */
    FAILED,

    /**
     * El pago ha sido cancelado de forma intencionada.
     *
     * Puede producirse por:
     * - Acción del usuario
     * - Decisión del sistema
     *
     * No se ha capturado ningún importe.
     */
    CANCELLED,

    /**
     * El pago ha expirado por inactividad
     * o por superar el tiempo límite permitido.
     *
     * Muy común en flujos de pago con redirección.
     */
    EXPIRED,

    /**
     * El pago ha sido reembolsado, total o parcialmente.
     *
     * Un reembolso siempre debe estar asociado
     * a un pago previamente COMPLETADO (SUCCESS).
     *
     * Este es un estado FINAL.
     */
    REFUNDED
}
