package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class TurnierController {

    @GetMapping("/")
    public String form() {
        return "form";
    }

    @PostMapping("/turnier")
    public String turnier(
            @RequestParam(required = false) String modus,
            @RequestParam(required = false) String maenner,
            @RequestParam(required = false) String frauen,
            @RequestParam(defaultValue = "1") int runden,
            Model model) {

        String safeModus = (modus == null) ? "" : modus;

        List<String> men = parse(maenner);
        List<String> women = parse(frauen);

        List<String[]> ergebnisse = new ArrayList<>();
        Set<String> played = new HashSet<>();
        Random rand = new Random();

        for (int r = 1; r <= runden; r++) {

            ergebnisse.add(new String[]{"ROUND", "Runde " + r, ""});

            Collections.shuffle(men, rand);
            Collections.shuffle(women, rand);

            // 🟡 MIXED DOPPEL (PERFEKT)
            if (safeModus.equals("mixed")) {

                Set<String> usedMen = new HashSet<>();
                Set<String> usedWomen = new HashSet<>();

                Collections.shuffle(men, rand);
                Collections.shuffle(women, rand);

                int size = Math.min(men.size(), women.size());

                List<String> availableMen = new ArrayList<>(men);
                List<String> availableWomen = new ArrayList<>(women);

                Collections.shuffle(availableMen, rand);
                Collections.shuffle(availableWomen, rand);

                for (int i = 0; i < size; i++) {

                    String man1 = null;
                    String woman1 = null;

                    // finde freien Mann + Frau
                    for (String m : availableMen) {
                        if (!usedMen.contains(m)) {
                            man1 = m;
                            break;
                        }
                    }

                    for (String w : availableWomen) {
                        if (!usedWomen.contains(w)) {
                            woman1 = w;
                            break;
                        }
                    }

                    if (man1 == null || woman1 == null) break;

                    usedMen.add(man1);
                    usedWomen.add(woman1);

                    String teamA = man1 + " + " + woman1;

                    // Gegner bilden
                    String man2 = null;
                    String woman2 = null;

                    for (String m : availableMen) {
                        if (!usedMen.contains(m)) {
                            man2 = m;
                            break;
                        }
                    }

                    for (String w : availableWomen) {
                        if (!usedWomen.contains(w)) {
                            woman2 = w;
                            break;
                        }
                    }

                    if (man2 == null || woman2 == null) break;

                    usedMen.add(man2);
                    usedWomen.add(woman2);

                    String teamB = man2 + " + " + woman2;

                    addMatch(ergebnisse, played, teamA, teamB);
                }
            }

            // 🔵 DOPPEL (Herren/Damen kombiniert)
            else if (safeModus.equals("herren") || safeModus.equals("damen") || safeModus.equals("doppel")) {

                List<String> all = new ArrayList<>();
                all.addAll(men);
                all.addAll(women);

                Collections.shuffle(all, rand);

                for (int i = 0; i + 3 < all.size(); i += 4) {

                    String teamA = all.get(i) + " + " + all.get(i + 1);
                    String teamB = all.get(i + 2) + " + " + all.get(i + 3);

                    addMatch(ergebnisse, played, teamA, teamB);
                }
            }

            // 🟢 EINZEL
            else if (safeModus.equals("einzel")) {

                List<String> all = new ArrayList<>();
                all.addAll(men);
                all.addAll(women);

                Collections.shuffle(all, rand);

                for (int i = 0; i + 1 < all.size(); i += 2) {

                    String a = all.get(i);
                    String b = all.get(i + 1);

                    addMatch(ergebnisse, played, a, b);
                }
            }
        }

        model.addAttribute("ergebnisse", ergebnisse);
        return "result";
    }

    // 🧼 sicherer Parser
    private List<String> parse(String input) {

        List<String> list = new ArrayList<>();

        if (input == null || input.isBlank()) return list;

        for (String s : input.split(",")) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }

        return list;
    }

    // 🧠 verhindert doppelte Spiele
    private void addMatch(List<String[]> list, Set<String> played, String a, String b) {

        if (a.equals(b)) return;

        String key = a + " vs " + b;
        String reverse = b + " vs " + a;

        if (played.contains(key) || played.contains(reverse)) return;

        played.add(key);

        list.add(new String[]{"", a, b});
    }
}