import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-char-counter',
  imports: [],
  templateUrl: './char-counter.html',
  styleUrl: './char-counter.css',
})
export class CharCounterComponent {
  @Input() text!: string;
  @Input() maxLength!: number;
}
