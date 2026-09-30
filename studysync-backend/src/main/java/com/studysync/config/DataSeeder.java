package com.studysync.config;

import com.studysync.entity.GoalTemplate;
import com.studysync.entity.TemplateSubject;
import com.studysync.entity.TemplateTopic;
import com.studysync.repository.GoalTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Seeds a curated set of common goal curricula (subjects + topics) so the
 * "Auto" goal-creation mode has real, useful data to work with out of the box.
 * This is deliberately a hand-curated static dataset (not an AI/LLM call) so the
 * feature is deterministic, free, and demo-safe.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final GoalTemplateRepository goalTemplateRepository;

    @Override
    public void run(String... args) {
        if (goalTemplateRepository.count() > 0) {
            return; // already seeded
        }

        seedUpsc();
        seedGateCs();
        seedPlacementDsa();
        seedBankPo();
        seedNetJrf();
        seedSscCgl();
        seedCatMba();
        seedNeetUg();
        seedJeeMain();
        seedIelts();
    }

    private void seedUpsc() {
        GoalTemplate template = GoalTemplate.builder()
                .name("UPSC CSE")
                .description("Civil Services Examination - Prelims + Mains preparation")
                .build();

        addSubject(template, "Indian Polity", 1.2, List.of(
                topic("Constitution: Preamble & Basic Structure", 6),
                topic("Union & State Executive", 5),
                topic("Parliament & State Legislature", 5),
                topic("Judiciary & Judicial Review", 4),
                topic("Fundamental Rights & DPSP", 5),
                topic("Local Government (Panchayati Raj)", 3)
        ));

        addSubject(template, "Indian History", 1.0, List.of(
                topic("Ancient India Overview", 4),
                topic("Medieval India Overview", 4),
                topic("Modern India: 1857-1947", 8),
                topic("Post-Independence Consolidation", 4)
        ));

        addSubject(template, "Geography", 1.0, List.of(
                topic("Physical Geography of India", 5),
                topic("World Geography Basics", 4),
                topic("Indian Climate & Monsoon System", 3),
                topic("Economic Geography & Resources", 4)
        ));

        addSubject(template, "Indian Economy", 1.1, List.of(
                topic("Basic Economic Concepts", 4),
                topic("Indian Economic Planning & Five Year Plans", 4),
                topic("Budget, Fiscal & Monetary Policy", 5),
                topic("Banking & Financial Institutions", 4),
                topic("Poverty, Employment & Inclusive Growth", 3)
        ));

        addSubject(template, "Environment & Ecology", 0.8, List.of(
                topic("Ecosystem & Biodiversity Basics", 4),
                topic("Climate Change & International Conventions", 4),
                topic("Environmental Pollution & Conservation", 3)
        ));

        addSubject(template, "CSAT (Aptitude)", 0.9, List.of(
                topic("Quantitative Aptitude Basics", 5),
                topic("Logical Reasoning", 4),
                topic("Reading Comprehension Practice", 4)
        ));

        addSubject(template, "Current Affairs", 1.0, List.of(
                topic("Monthly Current Affairs Compilation", 6),
                topic("Government Schemes & Policies", 4),
                topic("International Relations & Summits", 3)
        ));

        goalTemplateRepository.save(template);
    }

    private void seedGateCs() {
        GoalTemplate template = GoalTemplate.builder()
                .name("GATE CS")
                .description("GATE Computer Science & Information Technology preparation")
                .build();

        addSubject(template, "Data Structures & Algorithms", 1.3, List.of(
                topic("Arrays, Stacks, Queues", 4),
                topic("Linked Lists & Trees", 5),
                topic("Graphs & Graph Algorithms", 6),
                topic("Sorting & Searching", 4),
                topic("Dynamic Programming", 6)
        ));

        addSubject(template, "Operating Systems", 1.1, List.of(
                topic("Process Management & Scheduling", 5),
                topic("Memory Management & Paging", 5),
                topic("Deadlocks & Synchronization", 5),
                topic("File Systems", 3)
        ));

        addSubject(template, "DBMS", 1.1, List.of(
                topic("ER Model & Relational Model", 4),
                topic("Normalization", 4),
                topic("SQL Queries & Transactions", 5),
                topic("Indexing & File Organization", 4)
        ));

        addSubject(template, "Computer Networks", 1.0, List.of(
                topic("OSI & TCP/IP Model", 4),
                topic("Routing & Switching", 4),
                topic("Application Layer Protocols", 3),
                topic("Network Security Basics", 3)
        ));

        addSubject(template, "Theory of Computation", 0.9, List.of(
                topic("Finite Automata & Regular Languages", 5),
                topic("Context-Free Grammars & PDA", 5),
                topic("Turing Machines & Decidability", 4)
        ));

        addSubject(template, "Computer Organization & Architecture", 0.9, List.of(
                topic("Instruction Set & Addressing Modes", 4),
                topic("Pipelining", 4),
                topic("Memory Hierarchy & Cache", 4)
        ));

        addSubject(template, "Aptitude & Engineering Mathematics", 0.8, List.of(
                topic("Discrete Mathematics", 5),
                topic("Probability & Statistics", 4),
                topic("General Aptitude Practice", 3)
        ));

        goalTemplateRepository.save(template);
    }

    private void seedPlacementDsa() {
        GoalTemplate template = GoalTemplate.builder()
                .name("Campus Placement - DSA")
                .description("Data Structures & Algorithms prep for software engineering placement interviews")
                .build();

        addSubject(template, "Core Data Structures", 1.2, List.of(
                topic("Arrays & Strings", 5),
                topic("Linked List (Singly/Doubly/Circular)", 5),
                topic("Stacks & Queues", 4),
                topic("Trees & Binary Search Trees", 6),
                topic("Heaps & Priority Queues", 4),
                topic("Hash Maps & Sets", 4)
        ));

        addSubject(template, "Algorithms", 1.3, List.of(
                topic("Sorting Algorithms", 4),
                topic("Searching & Binary Search Variants", 4),
                topic("Recursion & Backtracking", 5),
                topic("Dynamic Programming", 7),
                topic("Greedy Algorithms", 4),
                topic("Graph Algorithms (BFS/DFS/Shortest Path)", 6)
        ));

        addSubject(template, "System Design Basics", 0.7, List.of(
                topic("Scalability Fundamentals", 3),
                topic("Database Design Basics", 3),
                topic("Common Design Patterns", 3)
        ));

        addSubject(template, "CS Fundamentals (OOP/DBMS/OS/CN)", 1.0, List.of(
                topic("OOP Concepts with Examples", 4),
                topic("DBMS: SQL & Normalization", 4),
                topic("OS: Process, Memory, Deadlock", 4),
                topic("Computer Networks Basics", 3)
        ));

        addSubject(template, "Aptitude & Communication", 0.8, List.of(
                topic("Quantitative Aptitude", 4),
                topic("Logical Reasoning", 3),
                topic("Group Discussion & HR Prep", 3)
        ));

        addSubject(template, "Mock Interviews & Resume", 0.9, List.of(
                topic("Resume Building & Project Explanation", 3),
                topic("Mock Technical Interviews", 5),
                topic("Behavioral Interview Practice", 3)
        ));

        goalTemplateRepository.save(template);
    }

    private void seedBankPo() {
        GoalTemplate template = GoalTemplate.builder()
                .name("Bank PO")
                .description("Bank Probationary Officer exam preparation (IBPS/SBI PO pattern)")
                .build();

        addSubject(template, "Quantitative Aptitude", 1.1, List.of(
                topic("Number Series & Simplification", 4),
                topic("Data Interpretation", 5),
                topic("Arithmetic Word Problems", 5),
                topic("Quadratic Equations & Inequalities", 3)
        ));

        addSubject(template, "Reasoning Ability", 1.1, List.of(
                topic("Puzzles & Seating Arrangement", 6),
                topic("Syllogism", 3),
                topic("Blood Relations & Direction Sense", 3),
                topic("Coding-Decoding", 3)
        ));

        addSubject(template, "English Language", 0.9, List.of(
                topic("Reading Comprehension", 4),
                topic("Grammar & Error Spotting", 4),
                topic("Vocabulary Building", 3)
        ));

        addSubject(template, "General & Banking Awareness", 1.0, List.of(
                topic("Banking Terms & RBI Functions", 4),
                topic("Current Affairs (last 6 months)", 5),
                topic("Static GK", 3)
        ));

        addSubject(template, "Computer Aptitude", 0.7, List.of(
                topic("Computer Fundamentals", 3),
                topic("MS Office Basics", 2),
                topic("Internet & Networking Basics", 2)
        ));

        goalTemplateRepository.save(template);
    }

    private void seedNetJrf() {
        GoalTemplate template = GoalTemplate.builder()
                .name("UGC NET Computer Science")
                .description("UGC NET/JRF Computer Science and Applications preparation")
                .build();

        addSubject(template, "Discrete Structures", 1.0, List.of(
                topic("Set Theory & Relations", 4),
                topic("Graph Theory", 4),
                topic("Combinatorics", 3)
        ));

        addSubject(template, "Programming & Data Structures", 1.1, List.of(
                topic("Programming Fundamentals (C/Java)", 4),
                topic("Data Structures Overview", 5)
        ));

        addSubject(template, "Computer Networks & OS", 1.0, List.of(
                topic("OS Concepts", 4),
                topic("Networking Layers & Protocols", 4)
        ));

        addSubject(template, "DBMS & Software Engineering", 1.0, List.of(
                topic("Database Concepts", 4),
                topic("SDLC Models", 3),
                topic("Software Testing", 3)
        ));

        addSubject(template, "Teaching & Research Aptitude (Paper 1)", 0.9, List.of(
                topic("Teaching Aptitude", 3),
                topic("Research Methodology", 3),
                topic("Reasoning & Comprehension", 3)
        ));

        goalTemplateRepository.save(template);
    }

    private void seedSscCgl() {
        GoalTemplate template = GoalTemplate.builder()
                .name("SSC CGL")
                .description("Staff Selection Commission Combined Graduate Level exam preparation")
                .build();

        addSubject(template, "Quantitative Aptitude", 1.1, List.of(
                topic("Number System & Simplification", 4),
                topic("Percentage, Profit & Loss", 4),
                topic("Time, Speed & Distance", 4),
                topic("Algebra & Geometry Basics", 5),
                topic("Data Interpretation", 4)
        ));
        addSubject(template, "General Intelligence & Reasoning", 1.0, List.of(
                topic("Analogies & Classification", 3),
                topic("Series & Coding-Decoding", 3),
                topic("Puzzles & Seating Arrangement", 5)
        ));
        addSubject(template, "English Comprehension", 0.9, List.of(
                topic("Reading Comprehension", 4),
                topic("Grammar, Cloze Test & Error Spotting", 4),
                topic("Vocabulary & One-Word Substitution", 3)
        ));
        addSubject(template, "General Awareness", 1.0, List.of(
                topic("Static GK (History, Polity, Geography)", 5),
                topic("Current Affairs (last 6 months)", 4),
                topic("Science Basics", 3)
        ));

        goalTemplateRepository.save(template);
    }

    private void seedCatMba() {
        GoalTemplate template = GoalTemplate.builder()
                .name("CAT MBA Entrance")
                .description("Common Admission Test preparation for MBA admissions")
                .build();

        addSubject(template, "Quantitative Ability", 1.2, List.of(
                topic("Arithmetic", 5),
                topic("Algebra", 5),
                topic("Geometry & Mensuration", 4),
                topic("Number Systems", 3)
        ));
        addSubject(template, "Verbal Ability & Reading Comprehension", 1.1, List.of(
                topic("Reading Comprehension Passages", 6),
                topic("Para-jumbles & Para-summary", 4),
                topic("Verbal Reasoning", 3)
        ));
        addSubject(template, "Data Interpretation & Logical Reasoning", 1.1, List.of(
                topic("Tables, Graphs & Caselets", 5),
                topic("Logical Reasoning Puzzles", 5),
                topic("Arrangements & Games", 4)
        ));
        addSubject(template, "Mock Tests & Strategy", 0.8, List.of(
                topic("Full-length Mock Tests", 6),
                topic("Sectional Time Management Practice", 3)
        ));

        goalTemplateRepository.save(template);
    }

    private void seedNeetUg() {
        GoalTemplate template = GoalTemplate.builder()
                .name("NEET UG")
                .description("National Eligibility cum Entrance Test for undergraduate medical admissions")
                .build();

        addSubject(template, "Physics", 1.1, List.of(
                topic("Mechanics", 6),
                topic("Thermodynamics & Kinetic Theory", 4),
                topic("Electrodynamics", 5),
                topic("Optics & Modern Physics", 5)
        ));
        addSubject(template, "Chemistry", 1.1, List.of(
                topic("Physical Chemistry Basics", 5),
                topic("Organic Chemistry Reactions", 6),
                topic("Inorganic Chemistry & Periodic Table", 5)
        ));
        addSubject(template, "Biology (Botany & Zoology)", 1.3, List.of(
                topic("Cell Biology & Genetics", 6),
                topic("Human Physiology", 6),
                topic("Plant Physiology", 4),
                topic("Ecology & Environment", 4),
                topic("Reproduction & Evolution", 5)
        ));

        goalTemplateRepository.save(template);
    }

    private void seedJeeMain() {
        GoalTemplate template = GoalTemplate.builder()
                .name("JEE Main")
                .description("Joint Entrance Examination for engineering admissions")
                .build();

        addSubject(template, "Physics", 1.1, List.of(
                topic("Mechanics", 6),
                topic("Electricity & Magnetism", 5),
                topic("Waves & Optics", 4),
                topic("Modern Physics", 4)
        ));
        addSubject(template, "Chemistry", 1.0, List.of(
                topic("Physical Chemistry", 5),
                topic("Organic Chemistry", 5),
                topic("Inorganic Chemistry", 5)
        ));
        addSubject(template, "Mathematics", 1.2, List.of(
                topic("Algebra", 5),
                topic("Calculus", 6),
                topic("Coordinate Geometry", 4),
                topic("Trigonometry", 3),
                topic("Probability & Statistics", 3)
        ));

        goalTemplateRepository.save(template);
    }

    private void seedIelts() {
        GoalTemplate template = GoalTemplate.builder()
                .name("IELTS")
                .description("International English Language Testing System preparation")
                .build();

        addSubject(template, "Listening", 0.9, List.of(
                topic("Section 1-2 Practice (Everyday Context)", 3),
                topic("Section 3-4 Practice (Academic Context)", 4)
        ));
        addSubject(template, "Reading", 1.0, List.of(
                topic("Skimming & Scanning Techniques", 3),
                topic("Academic Passage Practice", 5),
                topic("Question Types Practice (T/F/NG, Matching)", 4)
        ));
        addSubject(template, "Writing", 1.1, List.of(
                topic("Task 1: Graphs & Charts", 4),
                topic("Task 2: Essay Writing", 5),
                topic("Grammar & Vocabulary for Writing", 3)
        ));
        addSubject(template, "Speaking", 1.0, List.of(
                topic("Part 1: Introduction Practice", 2),
                topic("Part 2: Cue Card Practice", 3),
                topic("Part 3: Discussion Practice", 3)
        ));

        goalTemplateRepository.save(template);
    }

    private void addSubject(GoalTemplate template, String name, double weight, List<TemplateTopic> topics) {
        TemplateSubject subject = TemplateSubject.builder()
                .goalTemplate(template)
                .name(name)
                .weight(weight)
                .build();
        for (TemplateTopic t : topics) {
            t.setTemplateSubject(subject);
        }
        subject.setTopics(new ArrayList<>(topics));
        template.getSubjects().add(subject);
    }

    private TemplateTopic topic(String name, double hours) {
        return TemplateTopic.builder().name(name).estimatedHours(hours).build();
    }
}
