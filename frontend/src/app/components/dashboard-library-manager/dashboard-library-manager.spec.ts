import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardLibraryManager } from './dashboard-library-manager';

describe('DashboardLibraryManager', () => {
  let component: DashboardLibraryManager;
  let fixture: ComponentFixture<DashboardLibraryManager>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardLibraryManager]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DashboardLibraryManager);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
