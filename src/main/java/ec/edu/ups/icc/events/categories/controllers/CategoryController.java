package ec.edu.ups.icc.events.categories.controllers;

import ec.edu.ups.icc.events.categories.dtos.CategoryDTO;
import ec.edu.ups.icc.events.categories.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categorías", description = "Gestión de categorías de eventos")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Listar categorías", description = "Devuelve todas las categorías disponibles para usar en eventos.", responses = {
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDTO.class), examples = @ExampleObject(value = "[{\"id\":1,\"name\":\"Tecnología\",\"description\":\"Eventos de tecnología\"}]")))
    })
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Obtener categoría por id", description = "Retorna una categoría específica.", responses = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDTO.class), examples = @ExampleObject(value = "{\"id\":1,\"name\":\"Tecnología\",\"description\":\"Eventos de tecnología\"}"))),
            @ApiResponse(responseCode = "404", description = "No existe la categoría")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@Parameter(description = "Identificador de la categoría", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría. Requiere rol ADMIN.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "201", description = "Categoría creada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDTO.class), examples = @ExampleObject(value = "{\"id\":1,\"name\":\"Tecnología\",\"description\":\"Eventos de tecnología\"}"))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> createCategory(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos de la categoría", required = true, content = @Content(schema = @Schema(implementation = CategoryDTO.class), examples = @ExampleObject(value = "{\"name\":\"Tecnología\",\"description\":\"Eventos de tecnología\"}"))) @RequestBody CategoryDTO dto) {
        CategoryDTO created = categoryService.createCategory(dto);
        return ResponseEntity.created(URI.create("/api/categories/" + created.id())).body(created);
    }

    @Operation(summary = "Actualizar categoría", description = "Actualiza una categoría existente. Requiere rol ADMIN.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "Categoría actualizada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "No existe la categoría")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> updateCategory(@Parameter(description = "Identificador de la categoría") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Datos actualizados de la categoría", required = true) @RequestBody CategoryDTO dto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }

    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría. Requiere rol ADMIN.", security = @SecurityRequirement(name = "bearerAuth"), responses = {
            @ApiResponse(responseCode = "204", description = "Categoría eliminada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "No existe la categoría")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@Parameter(description = "Identificador de la categoría") @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
