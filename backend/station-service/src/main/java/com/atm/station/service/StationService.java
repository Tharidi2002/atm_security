package com.atm.station.service;

import com.atm.station.entity.Station;
import com.atm.station.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StationService {

    @Autowired
    private StationRepository stationRepository;

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    public Optional<Station> getStationById(Long id) {
        return stationRepository.findById(id);
    }

    public Optional<Station> getStationByPhoneNumber(String phoneNumber) {
        return stationRepository.findByPhoneNumber(phoneNumber);
    }

    public List<Station> getStationsByBankName(String bankName) {
        return stationRepository.findByBankName(bankName);
    }

    public List<Station> getActiveStations() {
        return stationRepository.findByIsActive(true);
    }

    public List<Station> searchStations(String keyword) {
        return stationRepository.findByBankNameOrLocationOrCityContaining(keyword, keyword, keyword);
    }

    public Station createStation(Station station) {
        if (stationRepository.existsByPhoneNumber(station.getPhoneNumber())) {
            throw new RuntimeException("Station with phone number " + station.getPhoneNumber() + " already exists");
        }
        station.setCreatedAt(LocalDateTime.now());
        station.setUpdatedAt(LocalDateTime.now());
        return stationRepository.save(station);
    }

    public Station updateStation(Long id, Station stationDetails) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found with id: " + id));

        if (!station.getPhoneNumber().equals(stationDetails.getPhoneNumber()) && 
            stationRepository.existsByPhoneNumber(stationDetails.getPhoneNumber())) {
            throw new RuntimeException("Station with phone number " + stationDetails.getPhoneNumber() + " already exists");
        }

        station.setPhoneNumber(stationDetails.getPhoneNumber());
        station.setBankName(stationDetails.getBankName());
        station.setLocation(stationDetails.getLocation());
        station.setAddress(stationDetails.getAddress());
        station.setCity(stationDetails.getCity());
        station.setBranchName(stationDetails.getBranchName());
        station.setIsActive(stationDetails.getIsActive());
        station.setModel(stationDetails.getModel());
        station.setSerialNumber(stationDetails.getSerialNumber());
        station.setUpdatedAt(LocalDateTime.now());

        return stationRepository.save(station);
    }

    public void deleteStation(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found with id: " + id));
        stationRepository.delete(station);
    }

    public Station toggleStationStatus(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found with id: " + id));
        
        station.setIsActive(!station.getIsActive());
        station.setUpdatedAt(LocalDateTime.now());
        
        return stationRepository.save(station);
    }

    public Optional<Station> findActiveStationByPhoneNumber(String phoneNumber) {
        return stationRepository.findActiveStationByPhoneNumber(phoneNumber);
    }
}
