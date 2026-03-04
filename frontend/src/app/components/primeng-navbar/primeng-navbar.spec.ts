import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PrimeNgNavBar } from './primeng-navbar';

describe('NavBar', () => {
  let component: PrimeNgNavBar;
  let fixture: ComponentFixture<PrimeNgNavBar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrimeNgNavBar],
    }).compileComponents();

    fixture = TestBed.createComponent(PrimeNgNavBar);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
