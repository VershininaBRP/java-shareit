package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;
    @NotBlank(message = "Название товара не должно быть пустым")
    private String name;
    @NotBlank(message = "Описание товара не должно быть пустым")
    private String description;
    private Boolean available;
    private Long requestId;
}
