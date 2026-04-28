import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AcceptDeclineReservationsPageComponent } from './accept-decline-reservations-page';

describe('AcceptDeclineReservationsPage', () => {
  let component: AcceptDeclineReservationsPageComponent;
  let fixture: ComponentFixture<AcceptDeclineReservationsPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AcceptDeclineReservationsPageComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AcceptDeclineReservationsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
