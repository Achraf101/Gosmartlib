import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NavBarHomeComponent } from './nav-bar-home';

describe('NavBarHomeComponent', () => {
  let component: NavBarHomeComponent;
  let fixture: ComponentFixture<NavBarHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavBarHomeComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(NavBarHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});