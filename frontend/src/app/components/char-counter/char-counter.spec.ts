import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CharCounterComponent } from './char-counter';

describe('CharCounter', () => {
  let component: CharCounterComponent;
  let fixture: ComponentFixture<CharCounterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CharCounterComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(CharCounterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
