import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavBarHome } from './nav-bar-home';

describe('NavBarHome', () => {
  let component: NavBarHome;
  let fixture: ComponentFixture<NavBarHome>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavBarHome]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavBarHome);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
