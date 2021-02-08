package org.c0g.nitunes.controllers;

import org.c0g.nitunes.dao.IMusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    @Autowired
    IMusicRepository musicRepository;

    @GetMapping("/")
    public String showIndex(Model model) {
        model.addAttribute("artists", musicRepository.getRandomArtists());
        model.addAttribute("genres", musicRepository.getRandomGenres());
        model.addAttribute("tracks", musicRepository.getRandomTracks());
        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam String term, Model model) {
        model.addAttribute("term", term);
        model.addAttribute("results", musicRepository.searchTracks(term));
        return "result";
    }
}
