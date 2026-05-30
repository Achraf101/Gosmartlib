import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AccordionModule } from 'primeng/accordion';
import { NavBarComponent } from '../nav-bar/nav-bar';
import { AuthService } from '../../services/auth';

interface FaqItem {
  question: string;
  answer: string;
}

@Component({
  selector: 'app-begeleiding',
  standalone: true,
  imports: [CommonModule, AccordionModule, NavBarComponent],
  templateUrl: './begeleiding.html',
  styleUrl: './begeleiding.css',
})
export class BegeleidingComponent implements OnInit {
  roles: string[] = [];

  studentFaq: FaqItem[] = [
    {
      question: 'Hoe leen ik een boek uit?',
      answer:
        'Ga naar het boek dat je wil uitlenen en klik op "Nu ontlenen" of "Toevoegen aan ontlening". Bij "Toevoegen aan ontlening" kan je meerdere boeken tegelijk aanvragen. Ga daarna naar je winkelmandje, kies een startdatum en bevestig je aanvraag. De bibliotheekbeheerder behandelt je aanvraag.',
    },
    {
      question: 'Hoe verleng ik een uitlening?',
      answer:
        'Ga naar "Mijn uitleningen" via de navigatiebalk. Bij je actieve uitleningen zie je een knop "Verlengen". Klik hierop en bevestig de nieuwe einddatum. Let op: je kan een uitlening maar een beperkt aantal keer verlengen.',
    },
    {
      question: 'Hoe schrijf ik een recensie?',
      answer:
        'Ga naar de detailpagina van een boek dat je hebt uitgeleend. Scroll naar beneden naar het gedeelte "Recensies" en klik op "Recensie schrijven". Geef een score en schrijf je mening.',
    },
    {
      question: 'Hoe maak ik een favorietenlijst aan?',
      answer:
        'Klik op het bladwijzer-icoontje op een boekpagina of boekkaart om een boek toe te voegen aan je favorieten. Je kan je favorieten bekijken via "Favorieten" in de navigatiebalk.',
    },
    {
      question: 'Hoe deel ik een lijst met iemand anders?',
      answer:
        'Ga naar je favorietenlijst en klik op de deelknop. Je krijgt een unieke link die je kan delen met anderen.',
    },
    {
      question: 'Wat doe ik als een boek niet beschikbaar is?',
      answer:
        'Als een boek niet beschikbaar is op jouw locatie, zie je de melding "Jouw locatie heeft dit boek niet of het is niet meer beschikbaar." Neem contact op met de bibliotheekbeheerder als je het boek toch wil uitlenen.',
    },
    {
      question: 'Hoe weet ik wanneer ik mijn boek moet terugbrengen?',
      answer:
        'Ga naar "Mijn uitleningen" via de navigatiebalk. Daar zie je per uitlening de einddatum. Als de datum rood gekleurd is, is de teruggavedatum overschreden.',
    },
    {
      question: 'Wat zijn challenges en hoe werken ze?',
      answer:
        'Challenges zijn maandelijkse leesdoelen die je kan behalen. Je behaalt een challenge door te voldoen aan de voorwaarden, zoals een bepaald aantal boeken uitlenen in een maand.',
    },
  ];

  teacherFaq: FaqItem[] = [
    {
      question: 'Hoe bekijk ik het rapport van een leerling?',
      answer:
        'Ga naar "Mijn klassen" via de navigatiebalk. Klik op een klas en selecteer een leerling. Je ziet een volledig rapport met uitgeleende boeken, recensies, favoriete genres en punctualiteit.',
    },
    {
      question: 'Hoe zie ik welke klassen ik heb?',
      answer:
        'Ga naar "Mijn klassen" via de navigatiebalk. Je ziet automatisch alle klassen waarvoor jij als leerkracht bent ingesteld via Smartschool.',
    },
    {
      question: 'Hoe zie ik de leerlingen in een klas?',
      answer:
        'Ga naar "Mijn klassen" en klik op een klas. Je ziet een lijst van alle leerlingen in die klas, gesorteerd op naam. Klik op een leerling om het rapport te bekijken.',
    },
    {
      question: 'Hoe voeg ik een boek toe aan de bibliotheek?',
      answer:
        'Ga naar "Boek toevoegen" via de navigatiebalk. Vul de gegevens in zoals titel, auteur, genre, taal en boektype. Je kan ook een ISBN ingeven waarna de gegevens automatisch worden opgezocht. Sla het boek op om het toe te voegen aan de catalogus.',
    },
    {
      question: 'Hoe doe ik een bulk upload van boeken via Excel?',
      answer:
        'Ga naar de bulk upload pagina. Download eerst de template en vul deze in met je boekgegevens. Elke rij is één boek. Je kan ISBN-nummers invullen waarna de gegevens automatisch worden opgezocht. Upload het bestand en controleer de preview. Boeken met ontbrekende gegevens worden apart weergegeven zodat je ze kan aanvullen.',
    },
  ];

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
  this.authService.currentUser$.subscribe(user => {
    this.roles = user?.roles ?? [];
  });
}
}