package es.marcha.backend.modules.order.domain.enums;

/**
 * Representa el estado del ciclo de vida de un pago.
 *
 * Un pago es un evento financiero inmutable.
 * Una vez que un pago alcanza un estado final, NO debe modificarse.
 * Cualquier nueva acci├│n financiera (reintento, reembolso, pago parcial)
 * debe representarse como un NUEVO registro de pago.
 */
public enum PaymentStatus {
    /**
     * El pago ha sido creado en el sistema,
     * pero TODAV├ìA no se ha enviado al proveedor de pago.
     *
     * No se ha realizado ninguna operaci├│n financiera.
     */
    CREATED,

    /**
     * El pago ha sido enviado al proveedor y
     * se encuentra actualmente en proceso.
     *
     * Com├║n en m├®todos de pago as├¡ncronos.
     */
    PENDING,

    /**
     * El importe del pago ha sido autorizado por el banco,
     * lo que significa que los fondos est├ín reservados
     * pero A├ÜN no han sido capturados.
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
     * Este pago nunca llegar├í a completarse.
     */
    FAILED,

    /**
     * El pago ha sido cancelado de forma intencionada.
     *
     * Puede producirse por:
     * - Acci├│n del usuario
     * - Decisi├│n del sistema
     *
     * No se ha capturado ning├║n importe.
     */
    CANCELLED,

    /**
     * El pago ha expirado por inactividad
     * o por superar el tiempo l├¡mite permitido.
     *
     * Muy com├║n en flujos de pago con redirecci├│n.
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