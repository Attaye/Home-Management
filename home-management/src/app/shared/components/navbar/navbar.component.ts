import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';

interface NavItem { label: string; path: string; icon: string; }

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
})
export class NavbarComponent {
  navItems: NavItem[] = [
    { label: 'Dashboard',  path: '/dashboard',  icon: '⊞' },
    { label: 'Kunden',     path: '/kunden',     icon: '👥' },
    { label: 'Ablesungen', path: '/ablesungen', icon: '📊' },
    { label: 'Reports',    path: '/reports',    icon: '📁' },
  ];

  constructor(public auth: AuthService) {}

  logout(): void { this.auth.logout(); }
}