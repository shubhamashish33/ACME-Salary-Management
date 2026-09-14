import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-icon',
  standalone: true,
  template: `
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      @switch (name) {
        @case ('overview') { <path d="M4 13h6V4H4v9Zm10 7h6v-9h-6v9ZM4 20h6v-3H4v3Zm10-13h6V4h-6v3Z" /> }
        @case ('employees') { <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm13 10v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" /> }
        @case ('upload') { <path d="M12 16V4m0 0L7 9m5-5 5 5M5 20h14" /> }
        @case ('search') { <circle cx="11" cy="11" r="7" /><path d="m20 20-4-4" /> }
        @case ('download') { <path d="M12 4v12m0 0 5-5m-5 5-5-5M5 20h14" /> }
        @case ('plus') { <path d="M12 5v14M5 12h14" /> }
        @case ('edit') { <path d="m4 20 4.5-1 10-10a2.1 2.1 0 0 0-3-3l-10 10L4 20Zm10-12 3 3" /> }
        @case ('close') { <path d="m6 6 12 12M18 6 6 18" /> }
        @case ('logout') { <path d="M10 17l5-5-5-5m5 5H3m10-8h6a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-6" /> }
        @case ('chevron') { <path d="m9 18 6-6-6-6" /> }
      }
    </svg>
  `,
  styles: [':host{display:inline-grid;width:1.15rem;height:1.15rem;flex:0 0 auto}:host svg{width:100%;height:100%}'],
})
export class IconComponent {
  @Input({ required: true }) name = '';
}
