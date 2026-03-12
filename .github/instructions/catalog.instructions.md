---
description: Usar cuando se trabaja con productos, categorías, subcategorías, atributos, variantes o reseñas
applyTo: 'src/main/java/es/marcha/backend/modules/catalog/**'
---

# Skill: Catálogo

Módulo ubicado en `modules/catalog/`. Gestiona toda la jerarquía del catálogo de productos.

## Modelo de dominio

```
Category
  └── Subcategory (ManyToOne → Category)

Product
  ├── ManyToOne  → Subcategory
  ├── ManyToMany → ProductAttrib        (tabla: product_attrib)
  └── OneToMany  → ProductVariant       (cascada completa)
        └── OneToMany → ProductVariantAttrib → ProductAttribValue

ProductAttrib
  └── OneToMany → ProductAttribValue

ProductReview
  ├── ManyToOne → Product
  └── ManyToOne → User
```

## Reglas de negocio — Productos

- **Precio efectivo**: `discountPrice > 0 ? discountPrice : price`. Aplicar siempre en cálculos de pedido.
- **Slug**: si no se proporciona al crear, generarlo automáticamente vía `ProductUtils.createSlug(name)`.
- El precio de un producto **no debe aceptarse desde el frontend** para crear pedidos. Siempre leer desde BBDD.
- Stock recomendado: gestionarlo en la variante (`ProductVariant.stock`), no en el producto base.

## Reglas de negocio — Variantes

- **Variante por defecto**: al marcar una variante como `isDefault = true`, retirar el flag de cualquier variante anterior que lo tuviera.
- **SKU único**: validar unicidad del SKU antes de persistir.
- **Atributos de variante**: los `attribValueIds` enviados deben pertenecer a atributos asignados al producto (`Product.attribs`). Lanzar `ProductAttribException` si no.
- **Sin duplicados de tipo**: no puede haber dos valores del mismo `ProductAttrib` en la misma `ProductVariant`.

## Reglas de negocio — Reseñas

- Un usuario solo puede dejar una reseña por producto. Validar antes de persistir.
- Solo usuarios con pedidos entregados del producto deberían poder reseñarlo (si se implementa verificación de compra).

## Excepciones

| Clase | Constantes clave |
|---|---|
| `ProductException` | `DEFAULT`, `NOT_FOUND`, `DUPLICATE_SLUG` |
| `ProductAttribException` | 21 constantes agrupadas por dominio (attrib, value, variant) |

Nunca lanzar `RuntimeException` genérica. Usar siempre la excepción del dominio correspondiente.

## Endpoints

| Método | Path | Descripción |
|---|---|---|
| GET/POST/PUT/DELETE | `/categories` | CRUD de categorías |
| GET/POST/PUT/DELETE | `/subcategories` | CRUD de subcategorías |
| GET/POST/PUT/DELETE | `/products` | CRUD de productos |
| GET/POST/PUT/DELETE | `/products/attribs` | Atributos de producto |
| GET/POST/PUT/DELETE | `/products/attribs/{id}/values` | Valores de atributo |
| GET/POST/PUT/DELETE | `/products/{productId}/variants` | Variantes |
| POST/DELETE | `/products/variants/{id}/attribs/{attribValueId}` | Atributos de variante |
| GET/POST/DELETE | `/reviews/product/{productId}` | Reseñas |

## Response enriquecida de producto

`ProductResponseDTO` devuelve la lista completa de `attribs` y `variants`. Usar el mapper correspondiente para no exponer la entidad directamente.

## Gestión de inventario

El módulo `InventoryService` gestiona movimientos de stock (`MovementType`). Al crear un pedido, `OrderService` llama a `InventoryService` para decrementar stock. Al cancelar/devolver, se incrementa. No modificar stock directamente fuera de `InventoryService`.
