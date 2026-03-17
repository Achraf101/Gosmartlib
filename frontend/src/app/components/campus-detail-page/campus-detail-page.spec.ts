import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CampusDetailPageComponent } from './campus-detail-page';

describe('CampusDetailPage', () => {
  let component: CampusDetailPageComponent;
  let fixture: ComponentFixture<CampusDetailPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CampusDetailPageComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(CampusDetailPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
