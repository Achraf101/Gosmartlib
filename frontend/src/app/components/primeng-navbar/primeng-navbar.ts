import { Component, inject, OnInit } from '@angular/core';
import { MenubarModule } from 'primeng/menubar';
import { MenuItem, MessageService } from 'primeng/api';

import { HelloService } from '../../services/hello';
import { Hello } from '../../models/hello';

@Component({
  selector: 'app-primeng-bar',
  standalone: true,
  templateUrl: './primeng-navbar.html',
  imports: [MenubarModule],
  styleUrl: './primeng-navbar.css',
})
export class PrimeNgNavBar {
  items: MenuItem[] | undefined;

  private messageService = inject(MessageService);
  private helloService = inject(HelloService);
  loadHellos(): void {
    this.helloService.getAll().subscribe({
      next: (data: Hello[]) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: data[0].msg,
          life: 3000,
        });
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Probleem met het opvragen van de data',
          life: 3000,
        });
      },
    });
  }

  ngOnInit() {
    this.items = [
      {
        label: 'Home',
        icon: 'pi pi-home',
      },
      {
        label: 'Features',
        icon: 'pi pi-star',
      },
      {
        label: 'Projects',
        icon: 'pi pi-search',
        items: [
          {
            label: 'Components',
            icon: 'pi pi-bolt',
          },
          {
            label: 'Blocks',
            icon: 'pi pi-server',
          },
          {
            label: 'UI Kit',
            icon: 'pi pi-pencil',
          },
          {
            label: 'Templates',
            icon: 'pi pi-palette',
            items: [
              {
                label: 'Apollo',
                icon: 'pi pi-palette',
              },
              {
                label: 'Ultima',
                icon: 'pi pi-palette',
              },
            ],
          },
        ],
      },
      {
        label: 'Test',
        icon: 'pi pi-play-circle',
        command: () => {
          this.loadHellos();
        },
      },
    ];
  }
}
