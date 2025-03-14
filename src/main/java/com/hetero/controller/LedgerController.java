package com.hetero.controller;


import com.hetero.models.Ledger;
import com.hetero.service.LedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ledger")
public class LedgerController {

    @Autowired
    private LedgerService ledgerService;


    @GetMapping
    public Ledger getLedger(){
        return ledgerService.getLedger();
    }

    @PostMapping
    public ResponseEntity<Ledger> addLedger(@RequestBody Ledger ledger){
        return ResponseEntity.ok(ledgerService.addLedger(ledger));
    }

    @PutMapping
    public ResponseEntity<Ledger> updateLedger(@RequestBody Ledger ledger){
        return ResponseEntity.ok(ledgerService.updateLedger(ledger));
    }

}
