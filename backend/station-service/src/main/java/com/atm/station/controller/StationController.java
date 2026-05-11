package com.atm.station.controller;

import com.atm.station.entity.Station;
import com.atm.station.service.StationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/stations")
public class StationController {

    @Autowired
    private StationService stationService;

    @GetMapping
    public ResponseEntity<List<Station>> getAllStations() {
        List<Station> stations = stationService.getAllStations();
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Station> getStationById(@PathVariable Long id) {
        Optional<Station> station = stationService.getStationById(id);
        return station.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<Station> getStationByPhoneNumber(@PathVariable String phoneNumber) {
        Optional<Station> station = stationService.getStationByPhoneNumber(phoneNumber);
        return station.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/bank/{bankName}")
    public ResponseEntity<List<Station>> getStationsByBank(@PathVariable String bankName) {
        List<Station> stations = stationService.getStationsByBankName(bankName);
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Station>> getActiveStations() {
        List<Station> stations = stationService.getActiveStations();
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Station>> searchStations(@RequestParam String keyword) {
        List<Station> stations = stationService.searchStations(keyword);
        return ResponseEntity.ok(stations);
    }

    @PostMapping
    public ResponseEntity<Station> createStation(@Valid @RequestBody Station station) {
        try {
            Station newStation = stationService.createStation(station);
            return ResponseEntity.status(HttpStatus.CREATED).body(newStation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Station> updateStation(@PathVariable Long id, @Valid @RequestBody Station stationDetails) {
        try {
            Station updatedStation = stationService.updateStation(id, stationDetails);
            return ResponseEntity.ok(updatedStation);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<Station> toggleStationStatus(@PathVariable Long id) {
        try {
            Station station = stationService.toggleStationStatus(id);
            return ResponseEntity.ok(station);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
        try {
            stationService.deleteStation(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/active/phone/{phoneNumber}")
    public ResponseEntity<Station> getActiveStationByPhoneNumber(@PathVariable String phoneNumber) {
        Optional<Station> station = stationService.findActiveStationByPhoneNumber(phoneNumber);
        return station.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
