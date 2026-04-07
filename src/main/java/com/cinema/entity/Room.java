package com.cinema.entity;

import jakarta.persistence.*;
import lombok.*;
import com.cinema.enums.RoomType;
import com.cinema.enums.RoomStatus;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String name;

    @Convert(converter = com.cinema.converter.RoomTypeConverter.class)
    private RoomType type;

    @Column(name = "total_rows")
    private Integer totalRows;

    @Column(name = "total_cols")
    private Integer totalCols;

    @Enumerated(EnumType.STRING)
    private RoomStatus status;
}
